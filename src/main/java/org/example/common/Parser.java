package org.example.common;

public abstract class Parser {

        private String path;

        public Parser(String path) {
            this.path = path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return this.path;
        }

    public abstract void parse();
}
