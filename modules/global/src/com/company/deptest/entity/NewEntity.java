package com.company.deptest.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;

@Table(name = "DEPTEST_NEW_ENTITY")
@Entity(name = "deptest$NewEntity")
public class NewEntity extends StandardEntity {
    private static final long serialVersionUID = 7502973446453983031L;

    @Column(name = "NAME")
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}