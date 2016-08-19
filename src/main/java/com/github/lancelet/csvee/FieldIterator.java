package com.github.lancelet.csvee;

interface FieldIterator {
    boolean hasNext();
    boolean next();
    boolean isFieldValid();
    CsvError.Type getErrorType();
    int getFieldStart();
    int getFieldEnd();
};
