/**
 * Copyright (c) 2011-2025, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord;

import com.jfinal.log.Log;
import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * TransactionExecutor 支持新版本事务方法 transaction(...)，独立于原有事务方法 tx(...)
 */
public class TransactionExecutor {

    static final Log log = Log.getLog(TransactionExecutor.class);

    @SuppressWarnings("unchecked")
    public <R> R execute(Config config, int isolation, TransactionAtom<R> atom) {
        Connection conn = config.getThreadLocalConnection();
        Transaction<R> transaction = config.getThreadLocalTransaction();
        BiConsumer<Transaction<?>, Object> onBeforeCommit = config.getOnBeforeTransactionCommit();

        if (conn != null) {     // 嵌套事务
            if (transaction == null) {
                throw new RuntimeException("老版本事务方法 tx(...) 中不能嵌套调用新版本事务方法 transaction(...)");
            }
            return handleNestedTransaction(conn, transaction, isolation, atom, onBeforeCommit);
        }

        boolean active = true;
        Integer originalIsolation = null;
        Boolean originalAutoCommit = null;
        try {
            conn = config.getConnection();
            // 新版本事务的 conn 需放入 Config 内的 ThreadLocal，否则新老版本事务方法在混合嵌套调用时无法正确协同
            config.setThreadLocalConnection(conn);

            originalIsolation = conn.getTransactionIsolation();
            if (originalIsolation != isolation) {
                conn.setTransactionIsolation(isolation);
            }

            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            transaction = new Transaction<>();
            config.setThreadLocalTransaction(transaction);

            R ret = atom.run(transaction);
            // 若返回值类型实现了 TransactionRollbackDecision 接口，可用于决定是否回滚事务
            if (ret instanceof TransactionRollbackDecision && ((TransactionRollbackDecision) ret).shouldRollback()) {
                transaction.rollback();
            }
            // 内层、外层调用 onBeforeCommit 处理各自的 ret 返回值
            if (onBeforeCommit != null && !transaction.shouldRollback()) {
                onBeforeCommit.accept(transaction, ret);
            }

            if (transaction.shouldRollback()) {
                active = false;                             // 必须前置，回滚异常时避免重复回滚
                conn.rollback();
            } else {
                conn.commit();
                active = false;                             // 必须后置，提交失败仍需回滚事务
                transaction.executeOnAfterCommit();         // 用于新版本事务方法 transaction(...)
                // config.executeCallbackAfterTxCommit();   // 仅用于老版本事务方法 tx(...)
            }

            return ret;

        } catch (Exception e) {
            if (active && conn != null) try {conn.rollback();} catch (Exception e1) {log.error(e1.getMessage(), e1);}

            // 异常回调，局部回调优先级高于全局回调
            if (transaction != null && transaction.getOnException() != null) {
                log.error(e.getMessage(), e);   // 未向上抛出异常需做日志
                return transaction.getOnException().apply(e);
            } else if (config.getOnTransactionException() != null) {
                log.error(e.getMessage(), e);   // 未向上抛出异常需做日志
                return (R) config.getOnTransactionException().apply(e);
            }

            throw e instanceof RuntimeException ? (RuntimeException) e : new ActiveRecordException(e);

        } finally {
            boolean closeOnException = true;
            try {
                if (conn != null) {
                    if (originalAutoCommit != null) {
                        conn.setAutoCommit(originalAutoCommit);
                    }

                    // 恢复为 originalIsolation
                    if (originalIsolation != null && originalIsolation != conn.getTransactionIsolation()) {
                        conn.setTransactionIsolation(originalIsolation);
                    }

                    closeOnException = false;
                    conn.close();
                }

            } catch (Exception e) {
                if (conn != null && closeOnException) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                        log.error(e1.getMessage(), e1);
                    }
                }
                log.error(e.getMessage(), e);

            } finally {
                config.removeThreadLocalConnection();
                config.removeThreadLocalTransaction();
                // config.removeCallbackAfterTxCommit();    // 仅用于老版本事务方法 tx(...)
            }
        }
    }

    private <R> R handleNestedTransaction(Connection conn, Transaction<R> transaction, int isolation, TransactionAtom<R> atom, BiConsumer<Transaction<?>, Object> onBeforeCommit) {
        Function<Exception, R> upperLevelOnException = transaction.getAndRemoveOnException();

        try {
            if (conn.getTransactionIsolation() < isolation) {
                conn.setTransactionIsolation(isolation);
            }

            R ret = atom.run(transaction);
            // 若返回值类型实现了 TransactionRollbackDecision 接口，可用于决定是否回滚事务
            if (ret instanceof TransactionRollbackDecision && ((TransactionRollbackDecision) ret).shouldRollback()) {
                transaction.rollback();
            }
            // 内层、外层调用 onBeforeCommit 处理各自的 ret 返回值
            if (onBeforeCommit != null && !transaction.shouldRollback()) {
                onBeforeCommit.accept(transaction, ret);
            }
            return ret;

        } catch (Exception e) {
            transaction.rollback();

            if (transaction.getOnException() != null) {
                transaction.getOnException().apply(e);  // 注意不要 return，需在后面抛出异常
            }

            throw e instanceof RuntimeException ? (RuntimeException) e : new ActiveRecordException(e);

        } finally {
            transaction.onException(upperLevelOnException);
        }
    }
}


