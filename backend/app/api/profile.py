from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.core.database import get_db
from app.models.user import User, UserProfile
from app.schemas.profile import ProfileResponse, ProfileUpdate
from app.schemas.onboarding import OnboardingRequest, OnboardingResponse

router = APIRouter(prefix="/profile", tags=["profile"])


@router.get("", response_model=ProfileResponse)
async def get_profile(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(UserProfile).where(UserProfile.user_id == current_user.id))
    profile = result.scalar_one_or_none()
    if profile is None:
        return ProfileResponse()
    return profile


@router.patch("", response_model=ProfileResponse)
async def update_profile(
    update: ProfileUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(UserProfile).where(UserProfile.user_id == current_user.id))
    profile = result.scalar_one_or_none()

    if profile is None:
        profile = UserProfile(user_id=current_user.id)
        db.add(profile)

    update_data = update.model_dump(exclude_unset=True, mode="json")
    for field, value in update_data.items():
        setattr(profile, field, value)

    await db.commit()
    await db.refresh(profile)
    return profile


@router.post("/onboarding", response_model=OnboardingResponse)
async def submit_onboarding(
    request: OnboardingRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(UserProfile).where(UserProfile.user_id == current_user.id))
    profile = result.scalar_one_or_none()

    if profile is None:
        profile = UserProfile(user_id=current_user.id)
        db.add(profile)

    profile.weight = request.weight
    profile.height = request.height
    profile.goal = request.goal.value
    profile.level = request.level.value
    profile.gender = request.gender
    profile.age = request.age
    profile.injuries = request.injuries
    profile.equipment = request.equipment
    profile.onboarding_completed = True
    profile.medical_disclaimer_accepted = request.medical_disclaimer_accepted

    await db.commit()

    return OnboardingResponse()
