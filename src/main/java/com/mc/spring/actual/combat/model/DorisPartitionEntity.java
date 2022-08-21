package com.mc.spring.actual.combat.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author macheng
 * @date 2022/7/5 16:51
 */
@Data
@Accessors(chain = true)
public class DorisPartitionEntity {
    private String partitionId;
    private String partitionName;
    private String visibleVersion;
    private String visibleVersionTime;
    private String state;
    private String partitionKey;
    private String range;
    private String distributionKey;
    private String buckets;
    private String replicationNum;
    private String storageMedium;
    private String cooldownTime;
    private String lastConsistencyCheckTime;
    private String dataSize;
    private String isInMemory;
    private String replicaAllocation;
}
