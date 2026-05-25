import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.core.config import settings

logger = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    yield
    # Shutdown


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.APP_NAME,
        lifespan=lifespan,
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    from app.api.auth import router as auth_router
    app.include_router(auth_router)
    from app.api.profile import router as profile_router
    app.include_router(profile_router)

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(request: Request, exc: RequestValidationError):
        body = await request.body()
        logger.error("422 on %s %s | body: %s | errors: %s",
                     request.method, request.url.path,
                     body.decode("utf-8", errors="replace"),
                     exc.errors())
        return JSONResponse(status_code=422, content={"detail": exc.errors()})

    return app


app = create_app()


@app.get("/health")
async def health():
    return {"status": "ok"}
