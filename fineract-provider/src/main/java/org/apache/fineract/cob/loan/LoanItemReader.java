/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cob.loan;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.exceptions.LoanAccountWasAlreadyLocked;
import org.apache.fineract.cob.exceptions.LoanReadException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class LoanItemReader implements ItemReader<Loan> {

    private final LoanRepository loanRepository;
    private List<Long> alreadyLockedAccounts;
    private List<Long> remainingData;
    private Long loanId;

    @BeforeStep
    public void beforeStep(@NotNull StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
        List<Long> loanIds = (List<Long>) executionContext.get(LoanCOBConstant.LOAN_IDS);
        alreadyLockedAccounts = (List<Long>) jobExecutionContext.get(LoanCOBConstant.ALREADY_LOCKED_LOAN_IDS);
        remainingData = new ArrayList<>(loanIds);
    }

    @Override
    public Loan read() throws Exception {
        try {
            if (remainingData.size() > 0) {
                loanId = remainingData.remove(0);
                if (alreadyLockedAccounts != null && alreadyLockedAccounts.remove(loanId)) {
                    throw new LoanAccountWasAlreadyLocked(loanId);
                }

                return loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
            }
        } catch (Exception e) {
            throw new LoanReadException(loanId, e);
        }
        return null;

    }

    @AfterStep
    public ExitStatus afterStep(@NotNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
