# Vendored JavaWebStack snapshots

This directory is a Maven file repository for the JavaWebStack snapshot
dependencies used by the backend. These artifacts are committed because they
are no longer available from the configured Sonatype snapshot repository.

Keep the Maven repository layout intact. The backend `pom.xml` resolves the
artifacts from this directory during local and Docker builds.
