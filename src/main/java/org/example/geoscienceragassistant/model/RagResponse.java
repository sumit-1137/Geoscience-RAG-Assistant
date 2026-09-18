package org.example.geoscienceragassistant.model;

import java.util.List;

public class RagResponse {

    private String answer;

    private List<Source> sources;


    public RagResponse() {
    }


    public RagResponse(
            String answer,
            List<Source> sources) {

        this.answer = answer;
        this.sources = sources;
    }


    public String getAnswer() {
        return answer;
    }


    public void setAnswer(String answer) {
        this.answer = answer;
    }


    public List<Source> getSources() {
        return sources;
    }


    public void setSources(
            List<Source> sources) {

        this.sources = sources;
    }


    public static class Source {

        private String fileName;

        private Integer chunkIndex;


        public Source() {
        }


        public Source(
                String fileName,
                Integer chunkIndex) {

            this.fileName = fileName;
            this.chunkIndex = chunkIndex;
        }


        public String getFileName() {
            return fileName;
        }


        public void setFileName(
                String fileName) {

            this.fileName = fileName;
        }


        public Integer getChunkIndex() {
            return chunkIndex;
        }


        public void setChunkIndex(
                Integer chunkIndex) {

            this.chunkIndex = chunkIndex;
        }

    }
}