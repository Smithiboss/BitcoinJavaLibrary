# Java Bitcoin Library

A Java-based implementation of the concepts from [*Programming Bitcoin*](https://github.com/jimmysong/programmingbitcoin) by Jimmy Song.  
This project reimagines the book's Python examples in Java — for learning and exploration of Bitcoin’s technical foundations.

---

# ⚠️ Disclaimer

**THIS PROJECT IS NOT PRODUCTION-READY.**

It is intended strictly for educational and experimental purposes.

Do **NOT** use this software in real Bitcoin transactions, wallets,
or any systems that handle real funds.

The cryptographic and networking code provided here is simplified
for learning and does not undergo professional security reviews.
**USE AT YOUR OWN RISK.**

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Running Tests](#running-tests)

---

## Overview

This is a **personal learning project** designed to deepen my understanding of:

- Bitcoin internals
- Cryptographic primitives (e.g. ECDSA)
- Peer-to-peer network protocols

Over time, I plan to implement more complex functionality of the Bitcoin protocol.

---

## Features

- Pure Java implementation (no JNI/native crypto libs)
- ECDSA (Elliptic Curve Digital Signature Algorithm)
- Create and verify Bitcoin transactions (except Taproot)
- Bitcoin message parsing and basic P2P handshakes
- Unit tests

---

## Running Tests

You can run all tests using Maven:

```bash
mvn clean test
