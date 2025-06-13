package ro.nn.qa.automation.terminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a field on an AS400 screen
 * Replaces TN5250j ScreenField functionality
 */
public class AS400Field {
    private static final Logger logger = LoggerFactory.getLogger(AS400Field.class);
    
    private int fieldId;
    private int startPosition;
    private int endPosition;
    private int length;
    private String value;
    private String label;
    private boolean isInputField;
    private boolean isProtected;
    private FieldType fieldType;
    
    public enum FieldType {
        INPUT,
        OUTPUT,
        LABEL,
        SEPARATOR,
        UNKNOWN
    }
    
    /**
     * Constructor for AS400Field
     */
    public AS400Field(int fieldId, int startPosition, int endPosition, int length, String value) {
        this.fieldId = fieldId;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.length = length;
        this.value = value != null ? value : "";
        this.isInputField = true; // Default to input field
        this.isProtected = false;
        this.fieldType = FieldType.INPUT;
        this.label = "";
    }
    
    // Getters and Setters
    public int getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }
    
    public int getStartPosition() {
        return startPosition;
    }
    
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }
    
    public int getEndPosition() {
        return endPosition;
    }
    
    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value != null ? value : "";
        logger.debug("Field {} value set to: {}", fieldId, this.value);
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }
    
    public boolean isInputField() {
        return isInputField;
    }
    
    public void setInputField(boolean inputField) {
        isInputField = inputField;
    }
    
    public boolean isProtected() {
        return isProtected;
    }
    
    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
    
    public FieldType getFieldType() {
        return fieldType;
    }
    
    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
    
    /**
     * Check if field is empty
     */
    public boolean isEmpty() {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Check if field contains specific text
     */
    public boolean contains(String text) {
        return value != null && value.contains(text);
    }
    
    /**
     * Get field value as string (trimmed)
     */
    public String getValueAsString() {
        return value != null ? value.trim() : "";
    }
    
    /**
     * Get field value as integer
     */
    public Integer getValueAsInteger() {
        try {
            String trimmedValue = getValueAsString();
            if (trimmedValue.isEmpty()) {
                return null;
            }
            return Integer.parseInt(trimmedValue);
        } catch (NumberFormatException e) {
            logger.warn("Cannot convert field value '{}' to integer", value);
            return null;
        }
    }
    
    /**
     * Get field value as double
     */
    public Double getValueAsDouble() {
        try {
            String trimmedValue = getValueAsString();
            if (trimmedValue.isEmpty()) {
                return null;
            }
            return Double.parseDouble(trimmedValue);
        } catch (NumberFormatException e) {
            logger.warn("Cannot convert field value '{}' to double", value);
            return null;
        }
    }
    
    /**
     * Check if field is numeric
     */
    public boolean isNumeric() {
        String trimmedValue = getValueAsString();
        if (trimmedValue.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(trimmedValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if field is a date field
     */
    public boolean isDateField() {
        return label != null && 
               (label.toLowerCase().contains("date") || 
                label.toLowerCase().contains("dt"));
    }
    
    /**
     * Check if field is required
     */
    public boolean isRequired() {
        return label != null && 
               (label.contains("*") || 
                label.toLowerCase().contains("required"));
    }
    
    /**
     * Validate field value
     */
    public boolean isValid() {
        if (isRequired() && isEmpty()) {
            logger.warn("Required field {} is empty", fieldId);
            return false;
        }
        
        if (isDateField() && !isEmpty()) {
            // Basic date validation - can be enhanced
            String val = getValueAsString();
            return val.matches("\\d{2}/\\d{2}/\\d{4}") || 
                   val.matches("\\d{4}-\\d{2}-\\d{2}") ||
                   val.matches("\\d{8}");
        }
        
        if (!isEmpty() && length > 0 && value.length() > length) {
            logger.warn("Field {} value length {} exceeds maximum {}", fieldId, value.length(), length);
            return false;
        }
        
        return true;
    }
    
    /**
     * Clear field value
     */
    public void clear() {
        setValue("");
    }
    
    /**
     * Get field position as row/column
     */
    public int getRow() {
        return startPosition / 80; // Assuming 80 character width
    }
    
    public int getColumn() {
        return startPosition % 80;
    }
    
    /**
     * Check if position is within this field
     */
    public boolean containsPosition(int position) {
        return position >= startPosition && position <= endPosition;
    }
    
    @Override
    public String toString() {
        return "AS400Field{" +
                "id=" + fieldId +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", position=" + startPosition + "-" + endPosition +
                ", length=" + length +
                ", type=" + fieldType +
                ", input=" + isInputField +
                ", protected=" + isProtected +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AS400Field field = (AS400Field) obj;
        return fieldId == field.fieldId &&
               startPosition == field.startPosition &&
               endPosition == field.endPosition;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(fieldId, startPosition, endPosition);
    }
}
