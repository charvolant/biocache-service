/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.biocache.dto;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A DTO bean that represents a single (facet) field result (SOLR)
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class FieldResultDTO {
    String label;
    Long count;

    /**
     * Constructor
     * 
     * @param fieldValue
     * @param count 
     */
    public FieldResultDTO(String fieldValue, long count) {
        this.label = fieldValue;
        this.count = count;
    }
    
    /**
     * Default constructor
     */
    public FieldResultDTO() {}
    
    @JsonIgnore
    public void setFieldValue(String fieldValue) {
        this.label = fieldValue;
    }
    
    @JsonIgnore
    public String getFieldValue() {
    	return label;
    }
    
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FieldResultDTO [count=" + count + ", label=" + label + "]";
	}
}
