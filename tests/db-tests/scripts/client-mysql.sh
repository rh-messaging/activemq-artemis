#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

<<<<<<<< HEAD:scripts/new-down.sh

# Use this script to generate the downstream branch:
# Usage example:

# ./scripts/new-down ENTMQBR-XXX commit1 commit2... commitN

export PRG_PATH=`dirname $0`
. $PRG_PATH/downstream-env.profile

git fetch $REDHAT_DOWNSTREAM
git checkout $REDHAT_DOWNSTREAM/$DOWNSTREAM_BRANCH -B $1

for i in "${@:2}"
do
    echo "$i"
    git cherry-pick -x $i
    OLD_MSG=$(git log --format=%B -n1)
    git commit --amend -m"$OLD_MSG" -m"downstream: $1"
done

git push origin-rh $1 -f
========
source ./container-define.sh

$CONTAINER_COMMAND exec -it mysql-artemis-test mysql ARTEMIS-TEST -u root --password=artemis
>>>>>>>> 6a8cd175dc6 (ARTEMIS-4401 improving JDBC Performance with Paging by a significant factor):tests/db-tests/scripts/client-mysql.sh
