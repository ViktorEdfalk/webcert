package se.inera.webcert.modules.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DraftValidationResponse {

    private DraftValidationStatus status;
    
    private List<DraftValidationMessage> messages = new ArrayList<DraftValidationMessage>();
           
    public DraftValidationResponse() {
        // TODO Auto-generated constructor stub
    }

    public DraftValidationStatus getStatus() {
        return status;
    }

    public void setStatus(DraftValidationStatus status) {
        this.status = status;
    }

    public List<DraftValidationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DraftValidationMessage> messages) {
        this.messages = messages;
    }

}
