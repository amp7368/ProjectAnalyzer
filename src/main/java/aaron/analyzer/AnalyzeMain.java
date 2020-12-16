package aaron.analyzer;

import aaron.analyzer.bridge.AllQuests;

import java.io.IOException;

public class AnalyzeMain {
    public static void main(String[] args) throws IOException {
        AllQuests.initialize("test.csv");
        AllQuests.print();
    }
}
