interface BrandLogoProps {
  className?: string;
}

export default function BrandLogo({
  className = "",
}: BrandLogoProps) {
  return (
    <img
      src="/public/images/brand/RebbyStore_logo.png"
      alt="RebbyStore"
      className={className}
    />
  );
}