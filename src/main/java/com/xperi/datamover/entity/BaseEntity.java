package com.xperi.datamover.entity;

import java.util.Date;

import lombok.Data;


@Data
public class BaseEntity {

  // Job creation timestamp
  private Date createdAt;

  // Job Created by
  private String createdBy;

  // Job data modification timestamp
  private Date updatedAt;

  // Job data modified by
  private String updatedBy;
}
