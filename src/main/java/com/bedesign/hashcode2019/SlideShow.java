package com.bedesign.hashcode2019;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingByConcurrent;

public class SlideShow {


    static String[] photoDirection;

    static String[][] photoTags;

    static int points = 0;

    static Map<String, Set<Integer>> inverseMap;

    static Set<Integer> computedIdx;

    public static void main(String[] args) throws IOException {
        String filename = "/Users/phoenix/Downloads/b_lovely_landscapes.txt";
        String output = filename + ".result.txt";
        String file = FileUtils.readFileToString(FileUtils.getFile(filename), "UTF-8");
        String[] lines = file.split("\n");
        int size = Integer.parseInt(lines[0]);

        photoDirection = new String[size];
        photoTags = new String[size][];
        for (int i = 1; i <= size; i++) {
            int idx = i - 1;
            String[] photo = lines[i].split(" ");
            photoDirection[idx] = photo[0];
            photoTags[idx] = ArrayUtils.subarray(photo, 2, photo.length);
        }

        inverseMap = new HashMap<>(size * 20);
        for (int i = 0; i < photoTags.length; i++) {
            final int idx = i;
            String[] photoTag = photoTags[i];
            asList(photoTag).forEach(tag ->
                    inverseMap.computeIfAbsent(tag, k -> new HashSet<>(20)).add(idx));

        }


        computedIdx = new HashSet<>(size);
        List<String> result = new ArrayList<>(size + 1);
        int currentIdx = 0;
        int i = 0;
        while(computedIdx.size() != photoTags.length) {
            computedIdx.add(currentIdx);
            result.add(String.valueOf(currentIdx));
            currentIdx = findBestMatch(currentIdx);
        }

        result.add(0, String.valueOf(result.size()));
        FileUtils.writeLines(new File(output), result);
        System.out.println("computed: " + computedIdx.size());
        System.out.println("score: " + points);
    }

    public static int findBestMatch(int idx) {
        String[] tagsInCurrentPhoto = photoTags[idx];
        ConcurrentMap<Integer, Long> entryToOccurrences = asList(tagsInCurrentPhoto).stream()
                .map(inverseMap::get)
                .flatMap(Set::stream)
                .filter(i -> !computedIdx.contains(i))
                .collect(groupingByConcurrent(identity(), counting()));
        Entry<Integer, Long>[] entries = entryToOccurrences.entrySet().toArray(new Entry[entryToOccurrences.size()]);

        int maxScoreIdx = -1;
        int maxScore = -1;
        int[] entryToScore = new int[entryToOccurrences.size()];
        for (int i = 0; i < entryToOccurrences.size(); i++) {
            Entry<Integer, Long> entry = entries[i];
            int photoIdx = entry.getKey();
            int score = score(idx, photoIdx, entry.getValue().intValue());
            entryToScore[i] = score;
            if (maxScore < score || maxScore == score && photoTags[photoIdx].length <= photoTags[maxScoreIdx].length) {
                maxScore = score;
                maxScoreIdx = photoIdx;
            }
        }

        if (maxScoreIdx == -1) {
            for (int i = 0; i < photoTags.length; i++) {
                if (computedIdx.contains(i) || i == idx) {//skip already computed
                    continue;
                }
                maxScoreIdx = i;
                break;
            }
        }
        points += maxScore == -1 ? 0 : maxScore;

        return maxScoreIdx;
    }

    private static int score(int idx, int dest, int occurrency) {
        int uncommon1 = photoTags[idx].length - occurrency;
        int uncommon2 = photoTags[dest].length  - occurrency;
        return IntStream.of(uncommon1, uncommon2, occurrency).min().getAsInt();
    }


}
