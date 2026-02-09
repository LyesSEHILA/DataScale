package com.cyberscale.backend.dto.builder;

import java.util.List;

public record TopologyRequest(
    String userId, 
    List<NodeDTO> nodes, 
    List<LinkDTO> links
) {}