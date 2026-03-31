# Bug report: detached versioned entity parameter triggers auto-flush transient check

## Summary

Hibernate ORM `7.2.1.Final` throws a `PropertyValueException` during query auto-flush when a detached entity with a generated identifier and an uninitialized `@Version` field is bound as a query parameter.

The failure happens while Hibernate decides whether the parameter binding is transient, not during result processing.

## Environment

- **Hibernate ORM**: `7.2.1.Final`
- **JPA**: `3.2.0`
- **Database**: H2 `2.4.240`
- **Java**: 26

## Steps to reproduce

1. Run `DetachedEntityParameterTest`.
2. Hibernate persists a `Trustee` and a `Customer`.
3. The `Trustee` is detached by loading it through a lazy association and closing the `EntityManager`.
4. A managed `Customer` is modified, forcing auto-flush.
5. A query is executed with the detached `Trustee` as `:trustee`.

## Actual result

The query fails before execution completes:

```text
org.hibernate.PropertyValueException: Detached entity with generated id '1' has an uninitialized version value 'null' for entity be.valuya.hibernate.param.detached.repro.Trustee.version
    at org.hibernate.persister.entity.AbstractEntityPersister.isTransient(...)
    at org.hibernate.query.internal.QueryParameterBindingsImpl.isTransientEntityBinding(...)
```

## Expected result

Hibernate should treat the detached `Trustee` as an existing entity instance and execute the query normally.

## Validation of the fix

I validated a patched Hibernate build (built from `hibernate-core-7.2.6.Final`) against this reproducer.

Result: the test passes with the patched core.

## Reproducer reference

- [`README.md`](README.md)
- [`DetachedEntityParameterTest`](src/test/java/be/valuya/hibernate/param/detached/repro/DetachedEntityParameterTest.java)
