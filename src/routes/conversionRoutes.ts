import { Router } from 'express';
import * as conversionController from '../controllers/conversionController';
import { authenticate } from '../middleware/auth';
import { apiLimiter } from '../middleware/rateLimiter';

const router = Router();

router.use(apiLimiter, authenticate);

router.post('/upgrade', conversionController.convertToPremium);
router.post('/downgrade', conversionController.downgradeToFree);

export default router;
