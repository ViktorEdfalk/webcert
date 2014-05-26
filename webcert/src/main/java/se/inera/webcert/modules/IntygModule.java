package se.inera.webcert.modules;

public class IntygModule implements Comparable<IntygModule> {

    private String id;

    private String label;

    private String description;

    private boolean fragaSvarAvailable;

    private String cssPath;

    private String scriptPath;

    public IntygModule(String id, String label, String description, boolean fragaSvarAvailable, String cssPath, String scriptPath) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.fragaSvarAvailable = fragaSvarAvailable;
        this.cssPath = cssPath;
        this.scriptPath = scriptPath;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFragaSvarAvailable() {
        return fragaSvarAvailable;
    }

    public String getCssPath() {
         return cssPath;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    @Override
    public int compareTo(IntygModule o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntygModule) {
            IntygModule other = (IntygModule) obj;
            return id.equals(other.id);
        } else {
            return false;
        }
    }
}
