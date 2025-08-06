package com.API.Documents_Management.Courriel.Entities;

import com.API.Documents_Management.Courriel.Enums.TypeStructure;

public interface Structure {
    TypeStructure getTypeStructure();
    Long getId();
    String getName();
}