import { Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../middleware/auth';

export const getTransactions = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { type, status, page = '1', limit = '10' } = req.query;

    const where: any = { userId: req.userId };

    if (type) {
      where.type = type;
    }

    if (status) {
      where.status = status;
    }

    const skip = (parseInt(page as string) - 1) * parseInt(limit as string);
    const take = parseInt(limit as string);

    const [transactions, total] = await Promise.all([
      prisma.transaction.findMany({
        where,
        skip,
        take,
        orderBy: { createdAt: 'desc' },
        include: {
          subscription: {
            select: {
              id: true,
              tier: true,
              status: true,
            },
          },
        },
      }),
      prisma.transaction.count({ where }),
    ]);

    res.json({
      status: 'success',
      data: {
        transactions,
        pagination: {
          total,
          page: parseInt(page as string),
          limit: parseInt(limit as string),
          totalPages: Math.ceil(total / parseInt(limit as string)),
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

export const getTransaction = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const transaction = await prisma.transaction.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
      include: {
        subscription: true,
        user: {
          select: {
            id: true,
            email: true,
            name: true,
          },
        },
      },
    });

    if (!transaction) {
      throw new AppError('Transaction not found', 404);
    }

    res.json({
      status: 'success',
      data: { transaction },
    });
  } catch (error) {
    next(error);
  }
};

export const createTransaction = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { subscriptionId, type, amount, currency, description, metadata } = req.body;

    const transaction = await prisma.transaction.create({
      data: {
        userId: req.userId!,
        subscriptionId,
        type,
        amount,
        currency: currency || 'USD',
        description,
        metadata,
        status: 'COMPLETED',
      },
      include: {
        subscription: true,
      },
    });

    res.status(201).json({
      status: 'success',
      data: { transaction },
    });
  } catch (error) {
    next(error);
  }
};
