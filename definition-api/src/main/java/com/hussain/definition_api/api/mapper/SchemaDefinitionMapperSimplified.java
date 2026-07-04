package com.hussain.definition_api.api.mapper;

import com.hussain.definition_api.api.dto.request.SchemaCreationRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ColumnRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ConstraintsRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ConstraintsRequest.PrimaryKeyRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ConstraintsRequest.UniqueKeyRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ConstraintsRequest.ForeignKeyRequest;
import com.hussain.definition_api.api.dto.request.SchemaCreationRequest.ConstraintsRequest.CheckConstraintRequest;
import com.hussain.definition_api.api.dto.response.SchemaCreationResponse;
import com.hussain.definition_api.core.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SchemaDefinitionMapperSimplified {

    // This is the method you need to update
    public SchemaDefinition toSchemaDefinition(SchemaCreationRequest request) {
        if (request == null) {
            return null;
        }

        return SchemaDefinition.builder()
                .schema(request.getSchema())
                .tableName(request.getTableName())
                .columns(toColumnDefinitions(request.getColumns()))
                .constraints(toConstraints(request.getConstraints()))
                .tableComment(request.getTableComment())
                .ifNotExists(request.isIfNotExists())
                .temporary(request.isTemporary())
                .version(request.getVersion())  // <-- ADD THIS LINE
                .build();
    }

    public SchemaCreationResponse toSchemaCreationResponse(SchemaCreationResult result) {
        if (result == null) {
            return null;
        }

        return SchemaCreationResponse.builder()
                .success(result.isSuccess())
                .correlationId(result.getCorrelationId())
                .schema(result.getSchema())
                .tableName(result.getTableName())
                .ddlStatements(result.getDdlStatements())
                .createdAt(result.getCreatedAt())
                .message(result.getMessage())
                .build();
    }

    private List<ColumnDefinition> toColumnDefinitions(List<ColumnRequest> columnRequests) {
        if (columnRequests == null) {
            return new ArrayList<>();
        }

        return columnRequests.stream()
                .map(this::toColumnDefinition)
                .collect(Collectors.toList());
    }

    private ColumnDefinition toColumnDefinition(ColumnRequest request) {
        if (request == null) {
            return null;
        }

        return ColumnDefinition.builder()
                .name(request.getName())
                .type(request.getType())
                .length(request.getLength())
                .precision(request.getPrecision())
                .scale(request.getScale())
                .nullable(request.isNullable())
                .unique(request.getUnique())
                .defaultValue(request.getDefaultValue())
                .comment(request.getComment())
                .expression(request.getExpression())
                .generated(request.isGenerated())
                .identity(request.isIdentity())
                .identityStartWith(request.getIdentityStartWith())
                .identityIncrementBy(request.getIdentityIncrementBy())
                .build();
    }

    private Constraints toConstraints(ConstraintsRequest request) {
        if (request == null) {
            return Constraints.builder().build();
        }

        return Constraints.builder()
                .primaryKeys(toPrimaryKeys(request.getPrimaryKeys()))
                .uniqueKeys(toUniqueKeys(request.getUniqueKeys()))
                .foreignKeys(toForeignKeys(request.getForeignKeys()))
                .checkConstraints(toCheckConstraints(request.getCheckConstraints()))
                .build();
    }

    private List<PrimaryKey> toPrimaryKeys(List<PrimaryKeyRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }

        return requests.stream()
                .map(this::toPrimaryKey)
                .collect(Collectors.toList());
    }

    private PrimaryKey toPrimaryKey(PrimaryKeyRequest request) {
        if (request == null) {
            return null;
        }

        return PrimaryKey.builder()
                .name(request.getName())
                .columns(request.getColumns())
                .build();
    }

    private List<UniqueKey> toUniqueKeys(List<UniqueKeyRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }

        return requests.stream()
                .map(this::toUniqueKey)
                .collect(Collectors.toList());
    }

    private UniqueKey toUniqueKey(UniqueKeyRequest request) {
        if (request == null) {
            return null;
        }

        return UniqueKey.builder()
                .name(request.getName())
                .columns(request.getColumns())
                .build();
    }

    private List<ForeignKey> toForeignKeys(List<ForeignKeyRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }

        return requests.stream()
                .map(this::toForeignKey)
                .collect(Collectors.toList());
    }

    private ForeignKey toForeignKey(ForeignKeyRequest request) {
        if (request == null) {
            return null;
        }

        return ForeignKey.builder()
                .name(request.getName())
                .columns(request.getColumns())
                .referencedTable(request.getReferencedTable())
                .referencedColumns(request.getReferencedColumns())
                .onDelete(request.getOnDelete())
                .onUpdate(request.getOnUpdate())
                .deferrable(request.isDeferrable())
                .initiallyDeferred(request.isInitiallyDeferred())
                .matchFull(request.isMatchFull())
                .matchPartial(request.isMatchPartial())
                .matchSimple(request.isMatchSimple())
                .build();
    }

    private List<CheckConstraint> toCheckConstraints(List<CheckConstraintRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }

        return requests.stream()
                .map(this::toCheckConstraint)
                .collect(Collectors.toList());
    }

    private CheckConstraint toCheckConstraint(CheckConstraintRequest request) {
        if (request == null) {
            return null;
        }

        return CheckConstraint.builder()
                .name(request.getName())
                .expression(request.getExpression())
                .build();
    }
}