package se.inera.webcert.modules.registry;

public class IntygModule implements Comparable<IntygModule> {

    private String id;
    
    private String label;
        
    private String description;
    
    private String scriptPath;
    
    public IntygModule(String id, String label, String description, String scriptPath) {
        super();
        this.id = id;
        this.label = label;
        this.description = description;
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
    
    public String getScriptPath() {
        return scriptPath;
    }

    @Override
    public int compareTo(IntygModule o) {
        return getLabel().compareTo(o.getLabel());
    }

}
