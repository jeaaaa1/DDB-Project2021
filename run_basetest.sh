#!/usr/bin/env bash
rm -rf $(pwd)/src/transaction/data
cd src/transaction
make clean

cd ../lockmgr
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


make runBaseTest