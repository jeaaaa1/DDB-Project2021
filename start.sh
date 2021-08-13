#!/usr/bin/env bash


cd src/lockmgr
make


cd ../transaction
make clean
make server
make client


make runregistry &
make runtm &
make runrmflights &
make runrmrooms &
make runrmcars &
make runrmcustomers &
make runwc &
