import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config';
import { AppError } from './errorHandler';

export interface AuthRequest extends Request {
  userId?: string;
  userTier?: string;
}

export const authenticate = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  try {
    const token = req.headers.authorization?.replace('Bearer ', '');

    if (!token) {
      throw new AppError('Authentication required', 401);
    }

    const decoded = jwt.verify(token, config.jwt.secret) as {
      userId: string;
      tier: string;
    };

    req.userId = decoded.userId;
    req.userTier = decoded.tier;

    next();
  } catch (error) {
    next(new AppError('Invalid or expired token', 401));
  }
};

export const requirePremium = async (
  req: AuthRequest,
  res: Response,
  next: NextFunction
) => {
  if (req.userTier !== 'PREMIUM') {
    return next(new AppError('Premium subscription required', 403));
  }
  next();
};
