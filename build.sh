#!/bin/bash

mvn assembly:assembly -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true
