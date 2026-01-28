import { Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AuthRequest } from '../middleware/auth';

export const getSubscriptionMetrics = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const [
      totalSubscriptions,
      activeSubscriptions,
      cancelledSubscriptions,
      suspiciousSubscriptions,
      premiumSubscriptions,
      freeSubscriptions,
    ] = await Promise.all([
      prisma.subscription.count(),
      prisma.subscription.count({ where: { status: 'ACTIVE' } }),
      prisma.subscription.count({ where: { status: 'CANCELLED' } }),
      prisma.subscription.count({ where: { isSuspicious: true } }),
      prisma.subscription.count({ where: { tier: 'PREMIUM', status: 'ACTIVE' } }),
      prisma.user.count({ where: { tier: 'FREE' } }),
    ]);

    res.json({
      status: 'success',
      data: {
        metrics: {
          total: totalSubscriptions,
          active: activeSubscriptions,
          cancelled: cancelledSubscriptions,
          suspicious: suspiciousSubscriptions,
          premium: premiumSubscriptions,
          free: freeSubscriptions,
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

export const getRevenueMetrics = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const transactions = await prisma.transaction.findMany({
      where: {
        status: 'COMPLETED',
        type: {
          in: ['SUBSCRIPTION_PAYMENT', 'UPGRADE'],
        },
      },
      select: {
        amount: true,
        currency: true,
        createdAt: true,
      },
    });

    const totalRevenue = transactions.reduce((sum, t) => sum + t.amount, 0);
    const averageTransactionValue = transactions.length > 0 ? totalRevenue / transactions.length : 0;

    // Calculate revenue by month
    const revenueByMonth: Record<string, number> = {};
    transactions.forEach((t) => {
      const month = t.createdAt.toISOString().substring(0, 7);
      revenueByMonth[month] = (revenueByMonth[month] || 0) + t.amount;
    });

    res.json({
      status: 'success',
      data: {
        metrics: {
          totalRevenue,
          totalTransactions: transactions.length,
          averageTransactionValue,
          revenueByMonth,
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserEngagementMetrics = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const [
      totalUsers,
      freeUsers,
      premiumUsers,
      newUsersLast30Days,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.user.count({ where: { tier: 'FREE' } }),
      prisma.user.count({ where: { tier: 'PREMIUM' } }),
      prisma.user.count({
        where: {
          createdAt: {
            gte: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
          },
        },
      }),
    ]);

    res.json({
      status: 'success',
      data: {
        metrics: {
          totalUsers,
          freeUsers,
          premiumUsers,
          newUsersLast30Days,
          premiumConversionRate: totalUsers > 0 ? (premiumUsers / totalUsers) * 100 : 0,
        },
      },
    });
  } catch (error) {
    next(error);
  }
};

export const getConversionMetrics = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const [totalUsers, totalConversions] = await Promise.all([
      prisma.user.count(),
      prisma.transaction.count({
        where: {
          type: 'UPGRADE',
          status: 'COMPLETED',
        },
      }),
    ]);

    const conversionRate = totalUsers > 0 ? (totalConversions / totalUsers) * 100 : 0;

    // Get conversions by month
    const conversions = await prisma.transaction.findMany({
      where: {
        type: 'UPGRADE',
        status: 'COMPLETED',
      },
      select: {
        createdAt: true,
      },
    });

    const conversionsByMonth: Record<string, number> = {};
    conversions.forEach((c) => {
      const month = c.createdAt.toISOString().substring(0, 7);
      conversionsByMonth[month] = (conversionsByMonth[month] || 0) + 1;
    });

    res.json({
      status: 'success',
      data: {
        metrics: {
          totalConversions,
          conversionRate,
          conversionsByMonth,
        },
      },
    });
  } catch (error) {
    next(error);
  }
};
