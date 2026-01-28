import { Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../middleware/auth';

export const getReminders = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { type, isRead } = req.query;

    const where: any = { userId: req.userId };

    if (type) {
      where.type = type;
    }

    if (isRead !== undefined) {
      where.isRead = isRead === 'true';
    }

    const reminders = await prisma.reminder.findMany({
      where,
      orderBy: { scheduledAt: 'desc' },
    });

    res.json({
      status: 'success',
      data: { reminders },
    });
  } catch (error) {
    next(error);
  }
};

export const createReminder = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { type, title, message, scheduledAt, metadata } = req.body;

    const reminder = await prisma.reminder.create({
      data: {
        userId: req.userId!,
        type,
        title,
        message,
        scheduledAt: new Date(scheduledAt),
        metadata,
      },
    });

    res.status(201).json({
      status: 'success',
      data: { reminder },
    });
  } catch (error) {
    next(error);
  }
};

export const updateReminder = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;
    const { title, message, scheduledAt } = req.body;

    const reminder = await prisma.reminder.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
    });

    if (!reminder) {
      throw new AppError('Reminder not found', 404);
    }

    const updates: any = {};
    if (title !== undefined) updates.title = title;
    if (message !== undefined) updates.message = message;
    if (scheduledAt !== undefined) updates.scheduledAt = new Date(scheduledAt);

    const updated = await prisma.reminder.update({
      where: { id: String(id) },
      data: updates,
    });

    res.json({
      status: 'success',
      data: { reminder: updated },
    });
  } catch (error) {
    next(error);
  }
};

export const markReminderAsRead = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const reminder = await prisma.reminder.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
    });

    if (!reminder) {
      throw new AppError('Reminder not found', 404);
    }

    const updated = await prisma.reminder.update({
      where: { id: String(id) },
      data: { isRead: true },
    });

    res.json({
      status: 'success',
      data: { reminder: updated },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteReminder = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const reminder = await prisma.reminder.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
    });

    if (!reminder) {
      throw new AppError('Reminder not found', 404);
    }

    await prisma.reminder.delete({
      where: { id: String(id) },
    });

    res.json({
      status: 'success',
      message: 'Reminder deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};
