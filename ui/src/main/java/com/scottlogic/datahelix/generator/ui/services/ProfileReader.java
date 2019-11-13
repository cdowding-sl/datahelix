package com.scottlogic.datahelix.generator.ui.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottlogic.datahelix.generator.ui.dtos.ProfileDTO;

import java.io.File;
import java.io.IOException;

public class ProfileReader
{
    public ProfileDTO read(File profileFile)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
            mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            JsonNode node = mapper.readTree(profileFile);
            return mapper.treeToValue(node, ProfileDTO.class);
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
