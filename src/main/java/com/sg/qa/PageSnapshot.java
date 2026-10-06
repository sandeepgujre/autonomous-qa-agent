package com.sg.qa;

import java.util.ArrayList;
import java.util.List;

public class PageSnapshot {

    private String url;
    private String title;
    private List<ElementSnapshot> elements;

    public PageSnapshot() {
        this.elements = new ArrayList<>();
    }

    public PageSnapshot(
            String url,
            String title,
            List<ElementSnapshot> elements) {

        this.url = url;
        this.title = title;
        this.elements = elements;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ElementSnapshot> getElements() {
        return elements;
    }

    public void setElements(
            List<ElementSnapshot> elements) {

        this.elements = elements;
    }

    public static class ElementSnapshot {

        private String tag;
        private String id;
        private String name;
        private String type;
        private String placeholder;
        private String text;
        private String ariaLabel;
        private String href;
        private boolean visible;
        private boolean enabled;

        public ElementSnapshot() {
        }

        public ElementSnapshot(
                String tag,
                String id,
                String name,
                String type,
                String placeholder,
                String text,
                String ariaLabel,
                String href,
                boolean visible,
                boolean enabled) {

            this.tag = tag;
            this.id = id;
            this.name = name;
            this.type = type;
            this.placeholder = placeholder;
            this.text = text;
            this.ariaLabel = ariaLabel;
            this.href = href;
            this.visible = visible;
            this.enabled = enabled;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAriaLabel() {
            return ariaLabel;
        }

        public void setAriaLabel(String ariaLabel) {
            this.ariaLabel = ariaLabel;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}