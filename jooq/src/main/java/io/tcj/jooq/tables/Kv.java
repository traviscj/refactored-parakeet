/*
 * This file is generated by jOOQ.
 */
package io.tcj.jooq.tables;


import io.tcj.jooq.DefaultSchema;
import io.tcj.jooq.Indexes;
import io.tcj.jooq.Keys;
import io.tcj.jooq.tables.records.KvRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Kv extends TableImpl<KvRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>kv</code>
     */
    public static final Kv KV = new Kv();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KvRecord> getRecordType() {
        return KvRecord.class;
    }

    /**
     * The column <code>kv.id</code>.
     */
    public final TableField<KvRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>kv.created_at</code>.
     */
    public final TableField<KvRecord, LocalDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP(3)"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>kv.updated_at</code>.
     */
    public final TableField<KvRecord, LocalDateTime> UPDATED_AT = createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP(3)"), SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>kv.feed_sync_id</code>.
     */
    public final TableField<KvRecord, Long> FEED_SYNC_ID = createField(DSL.name("feed_sync_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>kv.shard</code>.
     */
    public final TableField<KvRecord, Integer> SHARD = createField(DSL.name("shard"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>kv.ns</code>.
     */
    public final TableField<KvRecord, String> NS = createField(DSL.name("ns"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.inline("-", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>kv.k</code>.
     */
    public final TableField<KvRecord, String> K = createField(DSL.name("k"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>kv.v</code>.
     */
    public final TableField<KvRecord, String> V = createField(DSL.name("v"), SQLDataType.CLOB, this, "");

    private Kv(Name alias, Table<KvRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Kv(Name alias, Table<KvRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>kv</code> table reference
     */
    public Kv(String alias) {
        this(DSL.name(alias), KV);
    }

    /**
     * Create an aliased <code>kv</code> table reference
     */
    public Kv(Name alias) {
        this(alias, KV);
    }

    /**
     * Create a <code>kv</code> table reference
     */
    public Kv() {
        this(DSL.name("kv"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.KV_IDX_CA);
    }

    @Override
    public Identity<KvRecord, Long> getIdentity() {
        return (Identity<KvRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<KvRecord> getPrimaryKey() {
        return Keys.KEY_KV_PRIMARY;
    }

    @Override
    public List<UniqueKey<KvRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.KEY_KV_U_FSI, Keys.KEY_KV_U_NS_K);
    }

    @Override
    public Kv as(String alias) {
        return new Kv(DSL.name(alias), this);
    }

    @Override
    public Kv as(Name alias) {
        return new Kv(alias, this);
    }

    @Override
    public Kv as(Table<?> alias) {
        return new Kv(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Kv rename(String name) {
        return new Kv(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Kv rename(Name name) {
        return new Kv(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Kv rename(Table<?> name) {
        return new Kv(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv where(Condition condition) {
        return new Kv(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Kv where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Kv where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Kv where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Kv where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Kv whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
