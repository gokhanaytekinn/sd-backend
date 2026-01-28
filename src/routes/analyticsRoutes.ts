import { Router } from 'express';
import * as analyticsController from '../controllers/analyticsController';
import { authenticate, requirePremium } from '../middleware/auth';

const router = Router();

router.use(authenticate);

router.get('/subscriptions', analyticsController.getSubscriptionMetrics);
router.get('/revenue', requirePremium, analyticsController.getRevenueMetrics);
router.get('/engagement', analyticsController.getUserEngagementMetrics);
router.get('/conversions', requirePremium, analyticsController.getConversionMetrics);

export default router;
