import { Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../middleware/auth';

export const getSubscriptions = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { status, isSuspicious } = req.query;

    const where: any = { userId: req.userId };

    if (status) {
      where.status = status;
    }

    if (isSuspicious !== undefined) {
      where.isSuspicious = isSuspicious === 'true';
    }

    const subscriptions = await prisma.subscription.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            name: true,
          },
        },
      },
    });

    res.json({
      status: 'success',
      data: { subscriptions },
    });
  } catch (error) {
    next(error);
  }
};

export const getSubscription = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const subscription = await prisma.subscription.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            name: true,
          },
        },
        transactions: {
          orderBy: { createdAt: 'desc' },
          take: 10,
        },
      },
    });

    if (!subscription) {
      throw new AppError('Subscription not found', 404);
    }

    res.json({
      status: 'success',
      data: { subscription },
    });
  } catch (error) {
    next(error);
  }
};

export const createSubscription = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { tier, amount, currency, billingCycle } = req.body;

    const subscription = await prisma.subscription.create({
      data: {
        userId: req.userId!,
        tier,
        amount,
        currency: currency || 'USD',
        billingCycle: billingCycle || 'MONTHLY',
        status: 'ACTIVE',
      },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            name: true,
          },
        },
      },
    });

    // Update user tier
    await prisma.user.update({
      where: { id: req.userId },
      data: { tier },
    });

    res.status(201).json({
      status: 'success',
      data: { subscription },
    });
  } catch (error) {
    next(error);
  }
};

export const cancelSubscription = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const subscription = await prisma.subscription.findFirst({
      where: {
        id: String(id),
        userId: req.userId,
      },
    });

    if (!subscription) {
      throw new AppError('Subscription not found', 404);
    }

    const updated = await prisma.subscription.update({
      where: { id: String(id) },
      data: {
        status: 'CANCELLED',
        endDate: new Date(),
      },
    });

    res.json({
      status: 'success',
      data: { subscription: updated },
    });
  } catch (error) {
    next(error);
  }
};

export const getSuspiciousSubscriptions = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const subscriptions = await prisma.subscription.findMany({
      where: {
        isSuspicious: true,
        isApproved: false,
      },
      orderBy: { createdAt: 'desc' },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            name: true,
          },
        },
      },
    });

    res.json({
      status: 'success',
      data: { subscriptions },
    });
  } catch (error) {
    next(error);
  }
};

export const flagSubscriptionAsSuspicious = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const subscription = await prisma.subscription.update({
      where: { id: String(id) },
      data: {
        isSuspicious: true,
        suspiciousReason: reason,
        isApproved: false,
      },
    });

    res.json({
      status: 'success',
      data: { subscription },
    });
  } catch (error) {
    next(error);
  }
};

export const approveSubscription = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const { id } = req.params;

    const subscription = await prisma.subscription.update({
      where: { id: String(id) },
      data: {
        isApproved: true,
        approvedAt: new Date(),
        approvedBy: req.userId,
      },
    });

    res.json({
      status: 'success',
      data: { subscription },
    });
  } catch (error) {
    next(error);
  }
};
