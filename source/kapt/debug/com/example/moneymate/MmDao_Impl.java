package com.example.moneymate;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.EntityUpsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.Integer;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class MmDao_Impl implements MmDao {
  private final RoomDatabase __db;

  private final EntityUpsertAdapter<AccountEntity> __upsertAdapterOfAccountEntity;

  private final EntityUpsertAdapter<TxEntity> __upsertAdapterOfTxEntity;

  private final EntityUpsertAdapter<BudgetEntity> __upsertAdapterOfBudgetEntity;

  private final EntityUpsertAdapter<GoalEntity> __upsertAdapterOfGoalEntity;

  private final EntityUpsertAdapter<ProfileEntity> __upsertAdapterOfProfileEntity;

  private final EntityUpsertAdapter<SettingsEntity> __upsertAdapterOfSettingsEntity;

  private final EntityUpsertAdapter<DeletedEntity> __upsertAdapterOfDeletedEntity;

  public MmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertAdapterOfAccountEntity = new EntityUpsertAdapter<AccountEntity>(new EntityInsertAdapter<AccountEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `accounts` (`id`,`uid`,`name`,`lastFour`,`balanceCents`,`dirty`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final AccountEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getName());
        }
        if (entity.getLastFour() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getLastFour());
        }
        statement.bindLong(5, entity.getBalanceCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    }, new EntityDeleteOrUpdateAdapter<AccountEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `accounts` SET `id` = ?,`uid` = ?,`name` = ?,`lastFour` = ?,`balanceCents` = ?,`dirty` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final AccountEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getName());
        }
        if (entity.getLastFour() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getLastFour());
        }
        statement.bindLong(5, entity.getBalanceCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getId());
        }
      }
    });
    this.__upsertAdapterOfTxEntity = new EntityUpsertAdapter<TxEntity>(new EntityInsertAdapter<TxEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `transactions` (`id`,`uid`,`merchant`,`category`,`amountCents`,`date`,`accountId`,`dirty`,`receiptUrl`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final TxEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getMerchant() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getMerchant());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getCategory());
        }
        statement.bindLong(5, entity.getAmountCents());
        if (entity.getDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getDate());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getAccountId());
        }
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getReceiptUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindText(9, entity.getReceiptUrl());
        }
      }
    }, new EntityDeleteOrUpdateAdapter<TxEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `transactions` SET `id` = ?,`uid` = ?,`merchant` = ?,`category` = ?,`amountCents` = ?,`date` = ?,`accountId` = ?,`dirty` = ?,`receiptUrl` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final TxEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getMerchant() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getMerchant());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getCategory());
        }
        statement.bindLong(5, entity.getAmountCents());
        if (entity.getDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getDate());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getAccountId());
        }
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getReceiptUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindText(9, entity.getReceiptUrl());
        }
        if (entity.getId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindText(10, entity.getId());
        }
      }
    });
    this.__upsertAdapterOfBudgetEntity = new EntityUpsertAdapter<BudgetEntity>(new EntityInsertAdapter<BudgetEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `budgets` (`id`,`uid`,`category`,`limitCents`,`dirty`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final BudgetEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getCategory());
        }
        statement.bindLong(4, entity.getLimitCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    }, new EntityDeleteOrUpdateAdapter<BudgetEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `budgets` SET `id` = ?,`uid` = ?,`category` = ?,`limitCents` = ?,`dirty` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final BudgetEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getCategory());
        }
        statement.bindLong(4, entity.getLimitCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getId());
        }
      }
    });
    this.__upsertAdapterOfGoalEntity = new EntityUpsertAdapter<GoalEntity>(new EntityInsertAdapter<GoalEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `goals` (`id`,`uid`,`name`,`targetCents`,`savedCents`,`dirty`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final GoalEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getName());
        }
        statement.bindLong(4, entity.getTargetCents());
        statement.bindLong(5, entity.getSavedCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    }, new EntityDeleteOrUpdateAdapter<GoalEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `goals` SET `id` = ?,`uid` = ?,`name` = ?,`targetCents` = ?,`savedCents` = ?,`dirty` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final GoalEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getName());
        }
        statement.bindLong(4, entity.getTargetCents());
        statement.bindLong(5, entity.getSavedCents());
        final int _tmp = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getId());
        }
      }
    });
    this.__upsertAdapterOfProfileEntity = new EntityUpsertAdapter<ProfileEntity>(new EntityInsertAdapter<ProfileEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `profile` (`uid`,`name`,`email`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        if (entity.getUid() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getEmail());
        }
      }
    }, new EntityDeleteOrUpdateAdapter<ProfileEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `profile` SET `uid` = ?,`name` = ?,`email` = ? WHERE `uid` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ProfileEntity entity) {
        if (entity.getUid() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getUid());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getEmail());
        }
        if (entity.getUid() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getUid());
        }
      }
    });
    this.__upsertAdapterOfSettingsEntity = new EntityUpsertAdapter<SettingsEntity>(new EntityInsertAdapter<SettingsEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `settings` (`uid`,`currency`,`theme`,`hideBalances`,`budgetAlerts`,`dirty`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final SettingsEntity entity) {
        if (entity.getUid() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getUid());
        }
        if (entity.getCurrency() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getCurrency());
        }
        if (entity.getTheme() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getTheme());
        }
        final int _tmp = entity.getHideBalances() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.getBudgetAlerts() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        final int _tmp_2 = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp_2);
      }
    }, new EntityDeleteOrUpdateAdapter<SettingsEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `settings` SET `uid` = ?,`currency` = ?,`theme` = ?,`hideBalances` = ?,`budgetAlerts` = ?,`dirty` = ? WHERE `uid` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final SettingsEntity entity) {
        if (entity.getUid() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getUid());
        }
        if (entity.getCurrency() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getCurrency());
        }
        if (entity.getTheme() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getTheme());
        }
        final int _tmp = entity.getHideBalances() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.getBudgetAlerts() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        final int _tmp_2 = entity.getDirty() ? 1 : 0;
        statement.bindLong(6, _tmp_2);
        if (entity.getUid() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getUid());
        }
      }
    });
    this.__upsertAdapterOfDeletedEntity = new EntityUpsertAdapter<DeletedEntity>(new EntityInsertAdapter<DeletedEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `deleted` (`key`,`uid`,`collection`,`docId`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final DeletedEntity entity) {
        if (entity.getKey() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getKey());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getCollection() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getCollection());
        }
        if (entity.getDocId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getDocId());
        }
      }
    }, new EntityDeleteOrUpdateAdapter<DeletedEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `deleted` SET `key` = ?,`uid` = ?,`collection` = ?,`docId` = ? WHERE `key` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final DeletedEntity entity) {
        if (entity.getKey() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getKey());
        }
        if (entity.getUid() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUid());
        }
        if (entity.getCollection() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getCollection());
        }
        if (entity.getDocId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getDocId());
        }
        if (entity.getKey() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getKey());
        }
      }
    });
  }

  @Override
  public Object upsertAccounts(final List<AccountEntity> items,
      final Continuation<? super Unit> $completion) {
    if (items == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfAccountEntity.upsert(_connection, items);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object upsertTransactions(final List<TxEntity> items,
      final Continuation<? super Unit> $completion) {
    if (items == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfTxEntity.upsert(_connection, items);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object upsertBudgets(final List<BudgetEntity> items,
      final Continuation<? super Unit> $completion) {
    if (items == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfBudgetEntity.upsert(_connection, items);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object upsertGoals(final List<GoalEntity> items,
      final Continuation<? super Unit> $completion) {
    if (items == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfGoalEntity.upsert(_connection, items);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object upsertProfile(final ProfileEntity item,
      final Continuation<? super Unit> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfProfileEntity.upsert(_connection, item);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object upsertSettings(final SettingsEntity item,
      final Continuation<? super Unit> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfSettingsEntity.upsert(_connection, item);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object addTombstone(final DeletedEntity item,
      final Continuation<? super Unit> $completion) {
    if (item == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __upsertAdapterOfDeletedEntity.upsert(_connection, item);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object accounts(final String uid,
      final Continuation<? super List<AccountEntity>> $completion) {
    final String _sql = "SELECT * FROM accounts WHERE uid = ? ORDER BY name";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfLastFour = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastFour");
        final int _columnIndexOfBalanceCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "balanceCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<AccountEntity> _result = new ArrayList<AccountEntity>();
        while (_stmt.step()) {
          final AccountEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpLastFour;
          if (_stmt.isNull(_columnIndexOfLastFour)) {
            _tmpLastFour = null;
          } else {
            _tmpLastFour = _stmt.getText(_columnIndexOfLastFour);
          }
          final int _tmpBalanceCents;
          _tmpBalanceCents = (int) (_stmt.getLong(_columnIndexOfBalanceCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new AccountEntity(_tmpId,_tmpUid,_tmpName,_tmpLastFour,_tmpBalanceCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object dirtyAccounts(final String uid,
      final Continuation<? super List<AccountEntity>> $completion) {
    final String _sql = "SELECT * FROM accounts WHERE uid = ? AND dirty = 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfLastFour = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "lastFour");
        final int _columnIndexOfBalanceCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "balanceCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<AccountEntity> _result = new ArrayList<AccountEntity>();
        while (_stmt.step()) {
          final AccountEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpLastFour;
          if (_stmt.isNull(_columnIndexOfLastFour)) {
            _tmpLastFour = null;
          } else {
            _tmpLastFour = _stmt.getText(_columnIndexOfLastFour);
          }
          final int _tmpBalanceCents;
          _tmpBalanceCents = (int) (_stmt.getLong(_columnIndexOfBalanceCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new AccountEntity(_tmpId,_tmpUid,_tmpName,_tmpLastFour,_tmpBalanceCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object transactions(final String uid,
      final Continuation<? super List<TxEntity>> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE uid = ? ORDER BY date DESC, id DESC";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfMerchant = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "merchant");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final int _columnIndexOfAmountCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "amountCents");
        final int _columnIndexOfDate = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date");
        final int _columnIndexOfAccountId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "accountId");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final int _columnIndexOfReceiptUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "receiptUrl");
        final List<TxEntity> _result = new ArrayList<TxEntity>();
        while (_stmt.step()) {
          final TxEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpMerchant;
          if (_stmt.isNull(_columnIndexOfMerchant)) {
            _tmpMerchant = null;
          } else {
            _tmpMerchant = _stmt.getText(_columnIndexOfMerchant);
          }
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          final int _tmpAmountCents;
          _tmpAmountCents = (int) (_stmt.getLong(_columnIndexOfAmountCents));
          final String _tmpDate;
          if (_stmt.isNull(_columnIndexOfDate)) {
            _tmpDate = null;
          } else {
            _tmpDate = _stmt.getText(_columnIndexOfDate);
          }
          final String _tmpAccountId;
          if (_stmt.isNull(_columnIndexOfAccountId)) {
            _tmpAccountId = null;
          } else {
            _tmpAccountId = _stmt.getText(_columnIndexOfAccountId);
          }
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          final String _tmpReceiptUrl;
          if (_stmt.isNull(_columnIndexOfReceiptUrl)) {
            _tmpReceiptUrl = null;
          } else {
            _tmpReceiptUrl = _stmt.getText(_columnIndexOfReceiptUrl);
          }
          _item = new TxEntity(_tmpId,_tmpUid,_tmpMerchant,_tmpCategory,_tmpAmountCents,_tmpDate,_tmpAccountId,_tmpDirty,_tmpReceiptUrl);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object dirtyTransactions(final String uid,
      final Continuation<? super List<TxEntity>> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE uid = ? AND dirty = 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfMerchant = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "merchant");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final int _columnIndexOfAmountCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "amountCents");
        final int _columnIndexOfDate = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date");
        final int _columnIndexOfAccountId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "accountId");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final int _columnIndexOfReceiptUrl = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "receiptUrl");
        final List<TxEntity> _result = new ArrayList<TxEntity>();
        while (_stmt.step()) {
          final TxEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpMerchant;
          if (_stmt.isNull(_columnIndexOfMerchant)) {
            _tmpMerchant = null;
          } else {
            _tmpMerchant = _stmt.getText(_columnIndexOfMerchant);
          }
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          final int _tmpAmountCents;
          _tmpAmountCents = (int) (_stmt.getLong(_columnIndexOfAmountCents));
          final String _tmpDate;
          if (_stmt.isNull(_columnIndexOfDate)) {
            _tmpDate = null;
          } else {
            _tmpDate = _stmt.getText(_columnIndexOfDate);
          }
          final String _tmpAccountId;
          if (_stmt.isNull(_columnIndexOfAccountId)) {
            _tmpAccountId = null;
          } else {
            _tmpAccountId = _stmt.getText(_columnIndexOfAccountId);
          }
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          final String _tmpReceiptUrl;
          if (_stmt.isNull(_columnIndexOfReceiptUrl)) {
            _tmpReceiptUrl = null;
          } else {
            _tmpReceiptUrl = _stmt.getText(_columnIndexOfReceiptUrl);
          }
          _item = new TxEntity(_tmpId,_tmpUid,_tmpMerchant,_tmpCategory,_tmpAmountCents,_tmpDate,_tmpAccountId,_tmpDirty,_tmpReceiptUrl);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object transactionCount(final String uid,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM transactions WHERE uid = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final Integer _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp;
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object budgets(final String uid,
      final Continuation<? super List<BudgetEntity>> $completion) {
    final String _sql = "SELECT * FROM budgets WHERE uid = ? ORDER BY category";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final int _columnIndexOfLimitCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "limitCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<BudgetEntity> _result = new ArrayList<BudgetEntity>();
        while (_stmt.step()) {
          final BudgetEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          final int _tmpLimitCents;
          _tmpLimitCents = (int) (_stmt.getLong(_columnIndexOfLimitCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new BudgetEntity(_tmpId,_tmpUid,_tmpCategory,_tmpLimitCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object dirtyBudgets(final String uid,
      final Continuation<? super List<BudgetEntity>> $completion) {
    final String _sql = "SELECT * FROM budgets WHERE uid = ? AND dirty = 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final int _columnIndexOfLimitCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "limitCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<BudgetEntity> _result = new ArrayList<BudgetEntity>();
        while (_stmt.step()) {
          final BudgetEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          final int _tmpLimitCents;
          _tmpLimitCents = (int) (_stmt.getLong(_columnIndexOfLimitCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new BudgetEntity(_tmpId,_tmpUid,_tmpCategory,_tmpLimitCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object budgetFor(final String uid, final String category,
      final Continuation<? super BudgetEntity> $completion) {
    final String _sql = "SELECT * FROM budgets WHERE uid = ? AND LOWER(category) = LOWER(?) LIMIT 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _argIndex = 2;
        if (category == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, category);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final int _columnIndexOfLimitCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "limitCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final BudgetEntity _result;
        if (_stmt.step()) {
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          final int _tmpLimitCents;
          _tmpLimitCents = (int) (_stmt.getLong(_columnIndexOfLimitCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _result = new BudgetEntity(_tmpId,_tmpUid,_tmpCategory,_tmpLimitCents,_tmpDirty);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object goals(final String uid, final Continuation<? super List<GoalEntity>> $completion) {
    final String _sql = "SELECT * FROM goals WHERE uid = ? ORDER BY name";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfTargetCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "targetCents");
        final int _columnIndexOfSavedCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "savedCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<GoalEntity> _result = new ArrayList<GoalEntity>();
        while (_stmt.step()) {
          final GoalEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final int _tmpTargetCents;
          _tmpTargetCents = (int) (_stmt.getLong(_columnIndexOfTargetCents));
          final int _tmpSavedCents;
          _tmpSavedCents = (int) (_stmt.getLong(_columnIndexOfSavedCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new GoalEntity(_tmpId,_tmpUid,_tmpName,_tmpTargetCents,_tmpSavedCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object dirtyGoals(final String uid,
      final Continuation<? super List<GoalEntity>> $completion) {
    final String _sql = "SELECT * FROM goals WHERE uid = ? AND dirty = 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfTargetCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "targetCents");
        final int _columnIndexOfSavedCents = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "savedCents");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<GoalEntity> _result = new ArrayList<GoalEntity>();
        while (_stmt.step()) {
          final GoalEntity _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final int _tmpTargetCents;
          _tmpTargetCents = (int) (_stmt.getLong(_columnIndexOfTargetCents));
          final int _tmpSavedCents;
          _tmpSavedCents = (int) (_stmt.getLong(_columnIndexOfSavedCents));
          final boolean _tmpDirty;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp != 0;
          _item = new GoalEntity(_tmpId,_tmpUid,_tmpName,_tmpTargetCents,_tmpSavedCents,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object profile(final String uid, final Continuation<? super ProfileEntity> $completion) {
    final String _sql = "SELECT * FROM profile WHERE uid = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfEmail = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "email");
        final ProfileEntity _result;
        if (_stmt.step()) {
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final String _tmpEmail;
          if (_stmt.isNull(_columnIndexOfEmail)) {
            _tmpEmail = null;
          } else {
            _tmpEmail = _stmt.getText(_columnIndexOfEmail);
          }
          _result = new ProfileEntity(_tmpUid,_tmpName,_tmpEmail);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object settings(final String uid, final Continuation<? super SettingsEntity> $completion) {
    final String _sql = "SELECT * FROM settings WHERE uid = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCurrency = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "currency");
        final int _columnIndexOfTheme = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "theme");
        final int _columnIndexOfHideBalances = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "hideBalances");
        final int _columnIndexOfBudgetAlerts = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "budgetAlerts");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final SettingsEntity _result;
        if (_stmt.step()) {
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCurrency;
          if (_stmt.isNull(_columnIndexOfCurrency)) {
            _tmpCurrency = null;
          } else {
            _tmpCurrency = _stmt.getText(_columnIndexOfCurrency);
          }
          final String _tmpTheme;
          if (_stmt.isNull(_columnIndexOfTheme)) {
            _tmpTheme = null;
          } else {
            _tmpTheme = _stmt.getText(_columnIndexOfTheme);
          }
          final boolean _tmpHideBalances;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfHideBalances));
          _tmpHideBalances = _tmp != 0;
          final boolean _tmpBudgetAlerts;
          final int _tmp_1;
          _tmp_1 = (int) (_stmt.getLong(_columnIndexOfBudgetAlerts));
          _tmpBudgetAlerts = _tmp_1 != 0;
          final boolean _tmpDirty;
          final int _tmp_2;
          _tmp_2 = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp_2 != 0;
          _result = new SettingsEntity(_tmpUid,_tmpCurrency,_tmpTheme,_tmpHideBalances,_tmpBudgetAlerts,_tmpDirty);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object dirtySettings(final String uid,
      final Continuation<? super List<SettingsEntity>> $completion) {
    final String _sql = "SELECT * FROM settings WHERE uid = ? AND dirty = 1";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCurrency = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "currency");
        final int _columnIndexOfTheme = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "theme");
        final int _columnIndexOfHideBalances = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "hideBalances");
        final int _columnIndexOfBudgetAlerts = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "budgetAlerts");
        final int _columnIndexOfDirty = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "dirty");
        final List<SettingsEntity> _result = new ArrayList<SettingsEntity>();
        while (_stmt.step()) {
          final SettingsEntity _item;
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCurrency;
          if (_stmt.isNull(_columnIndexOfCurrency)) {
            _tmpCurrency = null;
          } else {
            _tmpCurrency = _stmt.getText(_columnIndexOfCurrency);
          }
          final String _tmpTheme;
          if (_stmt.isNull(_columnIndexOfTheme)) {
            _tmpTheme = null;
          } else {
            _tmpTheme = _stmt.getText(_columnIndexOfTheme);
          }
          final boolean _tmpHideBalances;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfHideBalances));
          _tmpHideBalances = _tmp != 0;
          final boolean _tmpBudgetAlerts;
          final int _tmp_1;
          _tmp_1 = (int) (_stmt.getLong(_columnIndexOfBudgetAlerts));
          _tmpBudgetAlerts = _tmp_1 != 0;
          final boolean _tmpDirty;
          final int _tmp_2;
          _tmp_2 = (int) (_stmt.getLong(_columnIndexOfDirty));
          _tmpDirty = _tmp_2 != 0;
          _item = new SettingsEntity(_tmpUid,_tmpCurrency,_tmpTheme,_tmpHideBalances,_tmpBudgetAlerts,_tmpDirty);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object tombstones(final String uid,
      final Continuation<? super List<DeletedEntity>> $completion) {
    final String _sql = "SELECT * FROM deleted WHERE uid = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        final int _columnIndexOfKey = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "key");
        final int _columnIndexOfUid = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "uid");
        final int _columnIndexOfCollection = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "collection");
        final int _columnIndexOfDocId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "docId");
        final List<DeletedEntity> _result = new ArrayList<DeletedEntity>();
        while (_stmt.step()) {
          final DeletedEntity _item;
          final String _tmpKey;
          if (_stmt.isNull(_columnIndexOfKey)) {
            _tmpKey = null;
          } else {
            _tmpKey = _stmt.getText(_columnIndexOfKey);
          }
          final String _tmpUid;
          if (_stmt.isNull(_columnIndexOfUid)) {
            _tmpUid = null;
          } else {
            _tmpUid = _stmt.getText(_columnIndexOfUid);
          }
          final String _tmpCollection;
          if (_stmt.isNull(_columnIndexOfCollection)) {
            _tmpCollection = null;
          } else {
            _tmpCollection = _stmt.getText(_columnIndexOfCollection);
          }
          final String _tmpDocId;
          if (_stmt.isNull(_columnIndexOfDocId)) {
            _tmpDocId = null;
          } else {
            _tmpDocId = _stmt.getText(_columnIndexOfDocId);
          }
          _item = new DeletedEntity(_tmpKey,_tmpUid,_tmpCollection,_tmpDocId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteAccount(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM accounts WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object wipeAccounts(final String uid, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM accounts WHERE uid = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteTransaction(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM transactions WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object wipeTransactions(final String uid, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM transactions WHERE uid = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteBudget(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM budgets WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object wipeBudgets(final String uid, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM budgets WHERE uid = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object deleteGoal(final String id, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM goals WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object wipeGoals(final String uid, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM goals WHERE uid = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object removeTombstone(final String key, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM deleted WHERE key = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (key == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, key);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object wipeTombstones(final String uid, final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM deleted WHERE uid = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (uid == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, uid);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
