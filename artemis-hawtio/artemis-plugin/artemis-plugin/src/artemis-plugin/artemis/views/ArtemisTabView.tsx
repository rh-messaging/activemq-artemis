/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { useEffect, useState } from 'react'
import { Tabs, Tab, TabTitleText } from '@patternfly/react-core';
import { ProducerTable } from '../producers/ProducerTable';
import { ConsumerTable } from '../consumers/ConsumerTable';
import { ConnectionsTable } from '../connections/ConnectionsTable';
import { SessionsTable } from '../sessions/SessionsTable';
import { AddressesTable } from '../addresses/AddressesTable';
import { ArtemisContext, useArtemisTree } from '../context';
import { Status } from '../status/Status';
import { Filter } from '../table/ArtemisTable';
import { QueuesView } from '../queues/QueuesView';


export type Broker = {
  columnStorageLocation?: string
}

export type Navigate = {
  search: Function
  filter?: Filter
}

export const ArtemisTabs: React.FunctionComponent = () => {

  const { tree, selectedNode, brokerNode, setSelectedNode, findAndSelectNode } = useArtemisTree();
  const [activeTabKey, setActiveTabKey] = useState<string | number>(0);
  const[searchFilter, setSearchFilter] = useState<Filter | undefined>();


  const handleTabClick = (event: React.MouseEvent<any> | React.KeyboardEvent | MouseEvent, tabIndex: string | number
  ) => {
    setSearchFilter(undefined);
    setActiveTabKey(tabIndex);
  };

  const handleSearch = (tab: number, filter: Filter) => {
      setSearchFilter(filter);
      setActiveTabKey(tab);
  };

  useEffect(() => {

  }, [searchFilter, activeTabKey])

  return (
    <ArtemisContext.Provider value={{ tree, selectedNode, brokerNode, setSelectedNode, findAndSelectNode }}>
        <Tabs activeKey={activeTabKey}
          onSelect={handleTabClick}
          aria-label="artemistabs" height={100}>
          <Tab eventKey={0} title={<TabTitleText>Status</TabTitleText>} aria-label="connections">
            {activeTabKey === 0 &&
              <Status/>
            }
          </Tab>
          <Tab eventKey={1} title={<TabTitleText>Connections</TabTitleText>} aria-label="connections">
            {activeTabKey === 1 &&
              <ConnectionsTable search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
          <Tab eventKey={2} title={<TabTitleText>Sessions</TabTitleText>} aria-label="sessions">
            {activeTabKey === 2 &&
              <SessionsTable search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
          <Tab eventKey={3} title={<TabTitleText>Producers</TabTitleText>} aria-label="producers">
            {activeTabKey === 3 &&
              <ProducerTable search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
          <Tab eventKey={4} title={<TabTitleText>Consumers</TabTitleText>} aria-label="consumers">
            {activeTabKey === 4 &&
              <ConsumerTable search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
          <Tab eventKey={5} title={<TabTitleText>Addresses</TabTitleText>} aria-label="addresses">
            {activeTabKey === 5 &&
              <AddressesTable search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
          <Tab eventKey={6} title={<TabTitleText>Queues</TabTitleText>} aria-label="consumers">
            {activeTabKey === 6 &&
              <QueuesView search={handleSearch} filter={searchFilter}/>
            }
          </Tab>
        </Tabs>
    </ArtemisContext.Provider>
  )

}
