# HHH-20320 Hibernate detached entity parameter reproducer

This project reproduces a `PropertyValueException` thrown during auto-flush when a detached `Trustee` entity is bound as a JPQL parameter while a managed `Customer` is dirty.

See https://hibernate.atlassian.net/browse/HHH-20320

## Reproducer

The core test is [`DetachedEntityParameterTest`](src/test/java/be/valuya/hibernate/param/detached/repro/DetachedEntityParameterTest.java).

It:

1. Persists a `Trustee` and a `Customer`.
2. Detaches the `Trustee` by loading it through `Customer#getTrustee()` in a separate `EntityManager`.
3. Marks a managed `Customer` dirty.
4. Executes:

```sql
select c from Customer c where c.trustee = :trustee
```

with the detached trustee as the parameter.

## Baseline failure

With Hibernate ORM `7.2.1.Final`, this test fails during auto-flush with:

```text
Detached entity with generated id '1' has an uninitialized version value 'null'
```

## Patched validation

I validated the fix from the patched Hibernate checkout by installing the built `hibernate-core-7.2.6.Final.jar` and matching POM into the local Maven repository, then running:

```bash
mvn -Dhibernate.version=7.2.6.Final -Dtest=DetachedEntityParameterTest test
```

The test passes with the patched core.

## Project layout

- **Entities**: `Customer` and `Trustee` in `src/main/java`
- **Persistence unit**: `src/test/resources/META-INF/persistence.xml`
- **Reproducer test**: `src/test/java/be/valuya/hibernate/param/detached/repro/DetachedEntityParameterTest.java`
