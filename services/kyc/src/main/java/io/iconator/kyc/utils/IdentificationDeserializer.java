package io.iconator.kyc.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.iconator.kyc.dto.Identification;
import io.iconator.kyc.dto.IdentificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdentificationDeserializer extends StdDeserializer<IdentificationResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(IdentificationDeserializer.class);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public IdentificationDeserializer() {
        this(null);
    }

    public IdentificationDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public IdentificationResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode identifications = node.get("identifications");

        List<Identification> identificationList = new ArrayList<>();

        if(identifications.isArray()) {
            for(JsonNode idNode : identifications) {
                Identification identification = new Identification();

                JsonNode idProcessNode = idNode.get("identificationprocess");
                identification.setKycUuid(UUID.fromString(idProcessNode.get("transactionnumber").asText()));
                identification.setResult(idProcessNode.get("result").asText());
                try {
                    identification.setIdentificationTime(format.parse(idProcessNode.get("identificationtime").asText()));
                } catch(ParseException e) {
                    LOG.error("Error parsing date", e);
                }

                identificationList.add(identification);
            }
        } else {
            LOG.debug("Not an array");
        }

        IdentificationResponse response = new IdentificationResponse();
        response.setIdentifications(identificationList);

        return response;
    }
}
