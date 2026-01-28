import { Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../middleware/auth';

export const convertToPremium = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { amount, currency, billingCycle } = req.body;

    const user = await prisma.user.findUnique({
      where: { id: req.userId },
    });

    if (!user) {
      throw new AppError('User not found', 404);
    }

    if (user.tier === 'PREMIUM') {
      throw new AppError('User is already premium', 400);
    }

    // Create premium subscription
    const subscription = await prisma.subscription.create({
      data: {
        userId: req.userId!,
        tier: 'PREMIUM',
        amount,
        currency: currency || 'USD',
        billingCycle: billingCycle || 'MONTHLY',
        status: 'ACTIVE',
      },
    });

    // Update user tier
    const updatedUser = await prisma.user.update({
      where: { id: req.userId },
      data: { tier: 'PREMIUM' },
      select: {
        id: true,
        email: true,
        name: true,
        tier: true,
      },
    });

    // Create conversion transaction
    await prisma.transaction.create({
      data: {
        userId: req.userId!,
        subscriptionId: subscription.id,
        type: 'UPGRADE',
        amount,
        currency: currency || 'USD',
        description: 'Conversion from Free to Premium',
        status: 'COMPLETED',
      },
    });

    res.json({
      status: 'success',
      data: {
        user: updatedUser,
        subscription,
      },
    });
  } catch (error) {
    next(error);
  }
};

export const downgradeToFree = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      include: {
        subscriptions: {
          where: {
            status: 'ACTIVE',
          },
        },
      },
    });

    if (!user) {
      throw new AppError('User not found', 404);
    }

    if (user.tier === 'FREE') {
      throw new AppError('User is already free tier', 400);
    }

    // Cancel all active subscriptions
    await prisma.subscription.updateMany({
      where: {
        userId: req.userId,
        status: 'ACTIVE',
      },
      data: {
        status: 'CANCELLED',
        endDate: new Date(),
      },
    });

    // Update user tier
    const updatedUser = await prisma.user.update({
      where: { id: req.userId },
      data: { tier: 'FREE' },
      select: {
        id: true,
        email: true,
        name: true,
        tier: true,
      },
    });

    res.json({
      status: 'success',
      data: {
        user: updatedUser,
      },
    });
  } catch (error) {
    next(error);
  }
};
