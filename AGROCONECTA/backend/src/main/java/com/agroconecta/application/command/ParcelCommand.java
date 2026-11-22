package com.agroconecta.application.command;

import java.math.BigDecimal;

public record ParcelCommand(String name, String location, BigDecimal area, String description) {
}
