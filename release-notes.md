## Javanna Release notes

## 1.1 - 2016 October 15

* clone array values before returning them. Ensures immutability.
* coerce numbers into the correct Number type when possible without loss of precision.
* improved error messages for cases where values within arrays are invalid.
* clone array used to create annotation. Also required for true immutability.
* accept any Collection value for arrays.
* accept Maps as valid values to create inner annotations (if members are valid).
* Ensure Javanna annotations are always readable by Javanna, even when types are not public.
* Added option to read annotations recursively (ensuring the result contains no annotation types).

## 1.0 - 2016 October 14

**First release.**

Support for introspecting annotations, creating annotations and getting the values of an annotation as a Map.