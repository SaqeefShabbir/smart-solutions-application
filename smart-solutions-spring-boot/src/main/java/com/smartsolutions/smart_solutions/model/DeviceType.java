package com.smartsolutions.smart_solutions.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "device_types", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "capabilities", columnDefinition = "jsonb")
    private String capabilitiesJson;

    @OneToMany(mappedBy = "type")
    private List<Device> devices = new ArrayList<>();

    // Helper method to work with capabilities as Map
    @Transient
    public Map<String, Object> getCapabilities() throws JsonProcessingException {
        return new ObjectMapper().readValue(capabilitiesJson, new TypeReference<Map<String, Object>>() {});
    }

    @Transient
    public void setCapabilities(Map<String, Object> capabilities) throws JsonProcessingException {
        this.capabilitiesJson = new ObjectMapper().writeValueAsString(capabilities);
    }
}