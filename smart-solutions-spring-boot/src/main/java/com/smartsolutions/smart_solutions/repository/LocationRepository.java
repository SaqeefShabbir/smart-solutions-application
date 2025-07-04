package com.smartsolutions.smart_solutions.repository;

import com.smartsolutions.smart_solutions.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository  extends JpaRepository<Location, Long> {
}
