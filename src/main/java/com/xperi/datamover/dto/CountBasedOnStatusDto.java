package com.xperi.datamover.dto;

import javax.persistence.Id;

import lombok.Data;

/** This is a DTO class to get count of status for subJob  */
@Data
public class CountBasedOnStatusDto {

  @Id private String id;
  private long total;
}
