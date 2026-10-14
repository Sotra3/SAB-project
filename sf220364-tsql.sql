USE [SABProjekat]
GO
/****** Object:  StoredProcedure [dbo].[SP_REWARD_USER_Reward]    Script Date: 7/14/2026 2:49:17 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SP_REWARD_USER_Reward]
    @UserId INT,
    @MovieId INT
AS
BEGIN
    SET NOCOUNT ON;
    IF (SELECT COUNT(*) FROM Rating WHERE UserId = @UserId) < 10
        RETURN;
    DECLARE @GlobalAvg FLOAT;
    SELECT @GlobalAvg = AVG(CAST(Value AS FLOAT)) 
    FROM Rating 
    WHERE MovieId = @MovieId AND UserId <> @UserId;
    IF (@GlobalAvg IS NULL OR @GlobalAvg >= 6)
        RETURN;
    IF EXISTS (
        SELECT 1 
        FROM MovieGenre mg
        WHERE mg.MovieId = @MovieId 
        AND (
            SELECT AVG(CAST(r.Value AS FLOAT))
            FROM Rating r
            JOIN MovieGenre mg2 ON r.MovieId = mg2.MovieId
            WHERE r.UserId = @UserId AND mg2.GenreId = mg.GenreId
        ) >= 8
    )
    BEGIN
        UPDATE [Users] 
        SET RewardCount = RewardCount + 1 
        WHERE Id = @UserId;
    END
END
GO

USE [SABProjekat]
GO

/****** Object:  Trigger [dbo].[TR_BLOCK_EXTREME]    Script Date: 7/14/2026 2:50:42 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE TRIGGER [dbo].[TR_BLOCK_EXTREME] 
   ON  [dbo].[Rating]
   AFTER INSERT,UPDATE
AS 
BEGIN
	SET NOCOUNT ON;
	DECLARE @kursor CURSOR
	DECLARE @UserId INT
	DECLARE @MovieId INT
	DECLARE @Value INT
	DECLARE @ExtremeCount INT
	DECLARE @NeutralCount INT

	SET @kursor = CURSOR FOR SELECT UserId, Value, MovieId FROM inserted
	OPEN @kursor
	FETCH NEXT FROM @kursor INTO @UserId, @Value, @MovieId

	WHILE @@FETCH_STATUS = 0
	BEGIN
		IF (@Value = 10 OR @Value = 1)
		BEGIN
			SELECT @ExtremeCount = COUNT(*)
			FROM Rating r
			JOIN MovieGenre mg ON r.MovieId = mg.MovieId
			WHERE r.UserId = @UserId
			  AND (r.Value = 10 OR r.Value = 1)
			  AND mg.GenreId IN (
			      SELECT mg2.GenreId FROM MovieGenre mg2 WHERE mg2.MovieId = @MovieId
			  );

			SELECT @NeutralCount = COUNT(*)
			FROM Rating r
			JOIN MovieGenre mg ON r.MovieId = mg.MovieId
			WHERE r.UserId = @UserId
			  AND r.Value IN (6, 7, 8)
			  AND mg.GenreId IN (
			      SELECT mg2.GenreId FROM MovieGenre mg2 WHERE mg2.MovieId = @MovieId
			  );

			IF @ExtremeCount > 3 AND @NeutralCount < 3
			BEGIN
				RAISERROR('Ne mozete dati ekstremnu ocenu - dostignut je limit u ovom zanru.', 16, 1);
				ROLLBACK TRANSACTION;
				CLOSE @kursor;
				DEALLOCATE @kursor;
				RETURN;
			END
		END

		FETCH NEXT FROM @kursor INTO @UserId, @Value, @MovieId
	END

	CLOSE @kursor
	DEALLOCATE @kursor
END
GO

CREATE TRIGGER [dbo].[TR_BLOCK_EXTREME] 
   ON  [dbo].[Rating]
   AFTER INSERT,UPDATE
AS 
BEGIN
	SET NOCOUNT ON;
	DECLARE @kursor CURSOR
	DECLARE @UserId INT
	DECLARE @MovieId INT
	DECLARE @Value INT
	DECLARE @ExtremeCount INT
	DECLARE @NeutralCount INT

	SET @kursor = CURSOR FOR SELECT UserId, Value, MovieId FROM inserted
	OPEN @kursor
	FETCH NEXT FROM @kursor INTO @UserId, @Value, @MovieId

	WHILE @@FETCH_STATUS = 0
	BEGIN
		IF (@Value = 10 OR @Value = 1)
		BEGIN
			SELECT @ExtremeCount = COUNT(*)
			FROM Rating r
			JOIN MovieGenre mg ON r.MovieId = mg.MovieId
			WHERE r.UserId = @UserId
			  AND (r.Value = 10 OR r.Value = 1)
			  AND mg.GenreId IN (
			      SELECT mg2.GenreId FROM MovieGenre mg2 WHERE mg2.MovieId = @MovieId
			  );

			SELECT @NeutralCount = COUNT(*)
			FROM Rating r
			JOIN MovieGenre mg ON r.MovieId = mg.MovieId
			WHERE r.UserId = @UserId
			  AND r.Value IN (6, 7, 8)
			  AND mg.GenreId IN (
			      SELECT mg2.GenreId FROM MovieGenre mg2 WHERE mg2.MovieId = @MovieId
			  );

			IF @ExtremeCount > 3 AND @NeutralCount < 3
			BEGIN
				RAISERROR('Ne mozete dati ekstremnu ocenu - dostignut je limit u ovom zanru.', 16, 1);
				ROLLBACK TRANSACTION;
				CLOSE @kursor;
				DEALLOCATE @kursor;
				RETURN;
			END
		END

		FETCH NEXT FROM @kursor INTO @UserId, @Value, @MovieId
	END

	CLOSE @kursor
	DEALLOCATE @kursor
END
GO


ALTER TABLE [dbo].[Rating] ENABLE TRIGGER [TR_BLOCK_EXTREME]
GO

USE [SABProjekat]
GO

/****** Object:  Trigger [dbo].[TR_UPDATE_MOVIE_TREND]    Script Date: 7/14/2026 2:51:06 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE TRIGGER [dbo].[TR_UPDATE_MOVIE_TREND]
ON [dbo].[Rating]
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Uzimamo sve ID-jeve filmova koji su menjani (i stari i novi)
    DECLARE @MovieIds TABLE (MovieId INT);
    INSERT INTO @MovieIds (MovieId)
    SELECT MovieId FROM inserted
    UNION
    SELECT MovieId FROM deleted;

    DECLARE @MID INT;
    DECLARE @Cursor CURSOR;
    SET @Cursor = CURSOR FOR SELECT MovieId FROM @MovieIds;
    OPEN @Cursor;
    FETCH NEXT FROM @Cursor INTO @MID;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @TotalAvg FLOAT, @TotalCount INT;
        DECLARE @Last5Avg FLOAT;
        DECLARE @NewStatus VARCHAR(50) = NULL;

        -- Ukupni podaci o filmu
        SELECT @TotalCount = COUNT(Value), @TotalAvg = AVG(CAST(Value AS FLOAT))
        FROM Rating WHERE MovieId = @MID;

        IF @TotalCount = 0
        BEGIN
            -- Nema ocena - status ostaje NULL
            UPDATE Movie SET Status = NULL WHERE Id = @MID;
        END
        ELSE
        BEGIN
            -- 2. Rising / Falling (najniza prioritet) - prosek poslednjih 5 ocena po datumu
            SELECT @Last5Avg = AVG(CAST(Value AS FLOAT))
            FROM (SELECT TOP 5 Value FROM Rating WHERE MovieId = @MID ORDER BY Date DESC) AS Last5;

            IF @Last5Avg >= (@TotalAvg + 1)
                SET @NewStatus = 'Rising';
            ELSE IF @Last5Avg <= (@TotalAvg - 1)
                SET @NewStatus = 'Falling';

            -- 3. Trending (pregazi Rising/Falling) - film je u TOP 3 po broju novih ocena u poslednjih 30 dana
            IF @MID IN (
                SELECT TOP 3 MovieId
                FROM Rating
                WHERE Date >= DATEADD(DAY, -30, GETDATE())
                GROUP BY MovieId
                ORDER BY COUNT(*) DESC
            )
            BEGIN
                -- provera da film uopste ima ocena u poslednjih 30 dana (da ne dobije Trending sa 0 novih ocena)
                IF EXISTS (SELECT 1 FROM Rating WHERE MovieId = @MID AND Date >= DATEADD(DAY, -30, GETDATE()))
                    SET @NewStatus = 'Trending';
            END

            -- 4. Classic (najjaci prioritet, pregazi sve ostalo)
            IF @TotalCount >= 3 AND @TotalAvg >= 8
                SET @NewStatus = 'Classic';

            UPDATE Movie SET Status = @NewStatus WHERE Id = @MID;
        END

        FETCH NEXT FROM @Cursor INTO @MID;
    END

    CLOSE @Cursor;
    DEALLOCATE @Cursor;
END
GO

ALTER TABLE [dbo].[Rating] ENABLE TRIGGER [TR_UPDATE_MOVIE_TREND]
GO
