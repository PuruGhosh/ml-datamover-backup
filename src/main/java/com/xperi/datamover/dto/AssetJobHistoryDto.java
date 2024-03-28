package com.xperi.datamover.dto;

import java.util.Date;

import com.xperi.datamover.constants.AssetJobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** This is a DTO class for maintaining status update history of parent Job  */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetJobHistoryDto {
	
	private AssetJobStatus status;
	private Date modifiedDate;
}
