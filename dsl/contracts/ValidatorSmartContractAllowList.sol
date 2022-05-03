/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

pragma solidity >=0.5.0;

import "./ValidatorSmartContractInterface.sol";

contract ValidatorSmartContractAllowList is ValidatorSmartContractInterface {

    uint constant MAX_VALIDATORS = 256;
    address[] private validators;

    constructor (address[] memory initialValidators) {
        require(initialValidators.length > 0, "no initial validators");
        require(initialValidators.length < MAX_VALIDATORS, "number of validators cannot be larger than 256");

        for (uint i = 0; i < initialValidators.length; i++) {
            validators.push(initialValidators[i]);
        }
    }

    function getValidators() override external view returns (address[] memory) {
        return validators;
    }

    function activate(address newValidator) external {
        require(newValidator != address(0), "cannot activate validator with address 0");

        for (uint i = 0; i < validators.length; i++) {
            require(newValidator != validators[i], "validator is already active");
        }

        validators.push(newValidator);
    }

    function deactivate(address oldValidator) external {
        require(validators.length > 1, "cannot deactivate last validator");

        for (uint i = 0; i < validators.length; i++) {
            if(oldValidator == validators[i]) {
                validators[i] = validators[validators.length - 1];
                validators.pop();
            }
        }
    }
}