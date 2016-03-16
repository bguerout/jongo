package org.jongo.model;

import java.util.ArrayList;
import java.util.List;

import org.jongo.marshall.jackson.oid.MongoObjectId;

public class TypeWithNested {
  public static class NestedDocument {
    private String name;
    private String value;
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public NestedDocument withName(String name) {
      this.name = name;
      return this;
    }
    public String getValue() {
      return value;
    }
    public void setValue(String value) {
      this.value = value;
    }
    public NestedDocument withValue(String value) {
      this.value = value;
      return this;
    }
  }
  
  @MongoObjectId
  private String id;
  private List<NestedDocument> nested = new ArrayList<NestedDocument>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public TypeWithNested withId(String id) {
    this.id = id;
    return this;
  }

  public List<NestedDocument> getNested() {
    return nested;
  }

  public void setNested(List<NestedDocument> nested) {
    this.nested = nested;
  }
  
  public TypeWithNested addNested(NestedDocument nested) {
    this.nested.add(nested);
    return this;
  }
}
