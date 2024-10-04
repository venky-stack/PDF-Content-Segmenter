package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PDFContentSegmenter {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java PDFContentSegmenter <input-pdf> <number-of-cuts>");
            System.exit(1);
        }

        String inputPdfPath = args[0];
        int numberOfCuts = Integer.parseInt(args[1]);

        try {
            PDDocument document = PDDocument.load(new File(inputPdfPath));

            List<Double> whitespacePositions = detectWhiteSpaces(document);
            List<Double> cutPositions = getLargestWhitespaces(whitespacePositions, numberOfCuts);

            segmentPDF(document, cutPositions);

            document.close();
        } catch (IOException e) {
            System.out.println("Error reading PDF: " + e.getMessage());
        }
    }


    private static List<Double> detectWhiteSpaces(PDDocument document) throws IOException {
        PDFTextStripperWithPosition stripper = new PDFTextStripperWithPosition();
        List<Double> whiteSpaces = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            stripper.getText(document); 

            List<Double> yPositions = stripper.getYPositions();
            for (int i = 1; i < yPositions.size(); i++) {
                double gap = yPositions.get(i) - yPositions.get(i - 1);
                if (gap > 15) {  
                    whiteSpaces.add(yPositions.get(i));
                }
            }
        }

        return whiteSpaces;
    }

    private static List<Double> getLargestWhitespaces(List<Double> whiteSpaces, int numberOfCuts) {
        whiteSpaces.sort(Collections.reverseOrder());  
        return whiteSpaces.subList(0, Math.min(numberOfCuts, whiteSpaces.size()));
    }

    private static void segmentPDF(PDDocument document, List<Double> cutPositions) throws IOException {
        for (int i = 0; i <= cutPositions.size(); i++) {
            PDDocument segment = new PDDocument();
            segment.addPage(document.getPage(i));
            String outputFileName = "output_segment_" + (i + 1) + ".pdf";
            segment.save(outputFileName);
            segment.close();
            System.out.println("Saved segment: " + outputFileName);
        }
    }
}
class PDFTextStripperWithPosition extends PDFTextStripper {

    private final List<Double> yPositions;

    public PDFTextStripperWithPosition() throws IOException {
        super();
        yPositions = new ArrayList<>();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        yPositions.add(text.getYDirAdj());
        super.processTextPosition(text);
    }

    public List<Double> getYPositions() {
        return yPositions;
    }
}

