#!/usr/bin/env bash
mvn exec:exec@doc-uploader-init && \
mvn exec:exec@doc-uploader
